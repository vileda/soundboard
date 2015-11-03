var wsUrl = 'ws://' + window.location.hostname + ':' + window.location.port;
var ws = new ReconnectingWebSocket(wsUrl + window.location.pathname + 'socket');

ws.onopen = function () {
    console.log('connected to WebSocket');
};

ws.onclose = function () {
    console.log('closed WebSocket');
};

var streaming = false;

var eventHandlers = {};

ws.onmessage = function (message) {
    var event = JSON.parse(message.data);
    var handlers = eventHandlers[event.event];
    handlers.forEach(function(el) {
        el(event);
    });
};


var addWebsocketEventHandler = function addWebsocketEventHandler(event, handler) {
    if(eventHandlers[event]) {
        eventHandlers[event].push(handler);
    }
    else {
        eventHandlers[event] = [handler];
    }
};

function playOnServer(url) {
    $.ajax({
        url: 'sounds',
        contentType: 'application/json',
        method: 'POST',
        data: url
    });
}

var PlayQueue = React.createClass({
    getInitialState: function() {
        return {queue: []};
    },
    enqueueEventHandler: function(e) {
        console.log(e);
        this.state.queue.push(e.title);
        this.setState({queue: this.state.queue});
    },
    killEventHandler: function() {
        this.setState({queue: []});
    },
    playedEventHandler: function(e) {
        console.log(e);
        var newQueue = [];
        var index = this.state.queue.indexOf(e.title);
        if (index !== -1) {
            this.state.queue.splice(index, 1);
        }
        this.setState({queue: this.state.queue});
    },
    componentDidMount: function() {
      addWebsocketEventHandler('enqueue', this.enqueueEventHandler);
      addWebsocketEventHandler('kill', this.killEventHandler);
      addWebsocketEventHandler('played', this.playedEventHandler);
    },
    render: function () {
        var queuedSounds = this.state.queue.map(function(sound) {
            if(sound) {
                return(
                    <button className="btn btn-info">{sound}</button>
                );
            }
        });

        return (
            <div className="playQueue">
                {queuedSounds}
            </div>
        );
    }
});

var SoundfileOption = React.createClass({
    handleClick: function(e) {
        e.preventDefault();
        if(this.props.handleClick) {
            this.props.handleClick(e);
            return;
        }
        if(!streaming) {
            playOnServer($(e.target).data('url'));
        }
        else {
            var id = 'audio_'+md5(this.props.soundfile.path);
            document.getElementById(id).play();
        }
    },
    render: function() {
        var id = 'sound_'+(this.props.category ?
                md5(this.props.category.name) :
                md5(this.props.soundfile.path));
        return (
            <li>
                <a className="btn btn-success btn-default"
                   id={id} onClick={this.handleClick} href="/" data-url={this.props.soundfile.path}>
                    {this.props.soundfile.title}
                </a>
                <audio preload="none" id={'audio_'+md5(this.props.soundfile.path)} src={'sounds/stream?url='+this.props.soundfile.path}></audio>
            </li>
        );
    }
});

var CategorySelect = React.createClass({
    getInitialState: function () {
      return {soundfileOptions: []};
    },
    render: function() {
        var soundfileOptions = this.props.category.soundfiles.map(function (soundfile) {
            return (
                <SoundfileOption category={this.props.category} soundfile={soundfile} />
            );
        }.bind(this));
        return (
            <div key={'option_ul_'+md5(this.props.category.name)} className="collapse well category-select" id={'cat_'+md5(this.props.category.name)}>
                <ul id={'option_'+md5(this.props.category.name)}>
                    {soundfileOptions}
                </ul>
            </div>
        );
    }
});

var AutoCompleteBox = React.createClass({
    render: function() {
        var nodes = this.props.list.map(function(item){
            return <SoundfileOption handleClick={this.props.handleClick} soundfile={item} />
        }.bind(this));
        return (
            <div id={this.props.id} className="autocompleteNodes well category-select hidden">
                {nodes}
            </div>
        );
    }
});

var RemotePlay = React.createClass({
   handleKeyUp: function(e) {
       console.log(e);
       if(e.keyCode === 13) {
           playOnServer($(e.target).val());
           e.target.value = '';
       }
   },
   render: function () {
       return (
           <label>
               Remote: <input onKeyUp={this.handleKeyUp} type="text" placeholder="URL" />
           </label>
       );
   }
});

var SoundSearch = React.createClass({
    getInitialState: function() {
        return {autocomplete: [], call: {latest:0, term:''}};
    },
    makeCall: function(term, current) {
        var searchUrl = "sounds/search?q="+encodeURIComponent(term);
        $.getJSON(searchUrl, function(data) {
                if (current == this.state.call.latest) {
                    var newPriority = this.state.call.latest - 1;
                    this.setState({autocomplete: data, call: {latest: newPriority, term:''} });
                }
            }.bind(this)
        );
    },
    handleKeyUp : function (e) {
        var k = e.target.value;
        if (k.length > 1) {
            var priority = this.state.call.latest+1;
            this.setState({call: {latest: priority, term: k }});
            $('.autocompleteNodes').removeClass('hidden');
        }
        if (k.length == 0 && this.state.autocomplete.length > 0 ) {
            this.setState({autocomplete: [], call: {latest:0, term:''}});
            $('.autocompleteNodes').addClass('hidden');
        }
        return false;
    },
    render: function() {
        // if the incoming state contains a search term with a real priority then make the async ajax/jsonp calls
        if (this.state.call.latest > 0 && this.state.call.term != '') {
            this.makeCall(this.state.call.term, this.state.call.latest);
        }
        return (
            <div className="searchbox">
                <label>
                    Search: <input id="search-input" type="text" placeholder="search" onKeyUp={this.handleKeyUp} />
                </label>
                <AutoCompleteBox id={this.props.id} handleClick={this.props.handleClick} list={this.state.autocomplete} />
            </div>
        );
    }
});

var CronJobForm = React.createClass({
    handleClick: function(e) {
        var url = $(e.target).data('url');
        $("#search-input").val(url);
        $('#cronjob-search-form').addClass('hidden');
        this.setState({url: url});
    },
    handleSubmit: function (e) {
        e.preventDefault();
        $.ajax({
            url: 'sounds/timer',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(
                {
                    filePath: this.state.url,
                    day: $('#dayofweek').val(),
                    hour: $('#hour').val(),
                    minute: $('#minute').val(),
                    second: $('#second').val()
                }
            )
        });
        $(e.target).find("input[type=text]").val('');
    },
    render: function() {
        return (
            <div id="cronjob-form" className="well collapse">
                <form onSubmit={this.handleSubmit} class="form">
                    <SoundSearch id="cronjob-search-form" handleClick={this.handleClick} />
                    <label>Day of week: <input id="dayofweek" type="text" /></label>
                    <label>Hour: <input id="hour" type="text" /></label>
                    <label>Minute: <input id="minute" type="text" /></label>
                    <label>Second: <input id="second" type="text" /></label>
                    <button type="submit" className="btn btn-default">Create!</button>
                </form>
            </div>
        );
    }
});

var CategorySelectPanel = React.createClass({
    getInitialState: function() {
        return {data: [], categorySelects: []};
    },
    componentDidMount: function() {
        $.ajax({
            url: this.props.url,
            dataType: 'json',
            cache: false,
            success: function(data) {
                this.setState({data: data});
            }.bind(this),
            error: function(xhr, status, err) {
                console.error(this.props.url, status, err.toString());
            }.bind(this)
        });
    },
    handleClick: function (e) {
        if(this.state.categorySelects[e.target.id]) return;

        this.state.data.forEach(function (category)  {
            if(this.state.categorySelects[e.target.id]) return;
            if(!(e.target.id.indexOf(md5(category.name)) > -1)) return;
            ReactDOM.render(
                <CategorySelect id={'cat_select_'+md5(category.name)} category={category} />,
                document.getElementById('cat_select_cont_'+md5(category.name)));
            $('#cat_'+md5(category.name)).collapse();
            this.state.categorySelects[e.target.id] = true;
        }.bind(this));
        this.setState({categorySelects: this.state.categorySelects});
    },
    render: function() {
        var categorySelects = this.state.data.map(function (category) {
            return (
                <li onClick={this.handleClick} id={'cat_select_key_'+md5(category.name)} key={'cat_select_key_'+md5(category.name)} className="dropdown">
                    <button id={'cat_btn_'+md5(category.name)} className="btn btn-primary btn-lg" type="button" data-toggle="collapse" data-target={'#cat_'+md5(category.name)} aria-expanded="false" aria-controls={'#cat_'+md5(category.name)}>
                        <span id={'cat_select_tile_'+md5(category.name)}>{category.name}</span> <span className="caret" id={'cat_select_cont_caret_'+md5(category.name)}></span>
                    </button>
                    <span id={'cat_select_cont_'+md5(category.name)}></span>
                </li>
            );
        }.bind(this));
        return (
            <div className="categorySelectPanel">
                <div className="row">
                    <div className="col-md-3">
                        <div className="col-md-12"><StreamingToggle /></div>
                        <div className="col-md-12"><SoundSearch id="search-form" /></div>
                        <div className="col-md-12"><RemotePlay /></div>
                    </div>
                    <div className="col-md-9">
                        <div className="col-md-12"><PlayQueue /></div>
                    </div>
                </div>
                <ul>{categorySelects}</ul>
            </div>
        );
    }
});

var StreamingToggle = React.createClass({
    handleChange: function(e) {
        streaming = $(e.target).is(":checked");
    },
    render: function() {
        return (
            <div className="checkbox">
                <label>
                    <input onChange={this.handleChange} type="checkbox" /> Enable streaming
                </label>
            </div>
        )
    }
});

var KillButton = React.createClass({
    handleClick: function(e) {
        e.preventDefault();
        if(streaming) {
            $.each($('audio'), function () {
                this.pause();
            });
        }
        else {
            $.ajax({
                url: 'sounds/kill',
                method: 'POST',
                contentType: 'application/json'
            });
        }
    },
    render: function() {
        return (
            <a className="danger" href="/kill" onClick={this.handleClick}>Kill</a>
        );
    }
});

ReactDOM.render(
    <CategorySelectPanel url="sounds" />,
    document.getElementById('content')
);

ReactDOM.render(
    <KillButton />,
    document.getElementById('kill-button')
);

ReactDOM.render(
    <CronJobForm />,
    document.getElementById('cronjob-toggle')
);
