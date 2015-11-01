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
        if(!streaming) {
            playOnServer($(e.target).data('url'));
        }
        else {
            var id = 'audio_'+CryptoJS.MD5(this.props.soundfile.path);
            document.getElementById(id).play();
        }
    },
    render: function() {
        var id = 'sound_'+(this.props.category ?
                CryptoJS.MD5(this.props.category.name) :
                CryptoJS.MD5(this.props.soundfile.path));
        return (
            <li>
                <a className="btn btn-success btn-default"
                   id={id} onClick={this.handleClick} href="/" data-url={this.props.soundfile.path}>
                    {this.props.soundfile.title}
                </a>
                <audio preload="none" id={'audio_'+CryptoJS.MD5(this.props.soundfile.path)} src={'sounds/stream?url='+this.props.soundfile.path}></audio>
            </li>
        );
    }
});

var CategorySelect = React.createClass({
    render: function() {
        var soundfileOptions = this.props.category.soundfiles.map(function (soundfile) {
            return (
                <SoundfileOption category={this.props.category} soundfile={soundfile} />
            );
        }.bind(this));
        return (
            <div className="collapse well category-select" id={'cat_'+CryptoJS.MD5(this.props.category.name)}>
                <ul>
                    {soundfileOptions}
                </ul>
            </div>
        );
    }
});

var AutoComplete = React.createClass({
    render: function() {
        return (
            <p><a className="btn btn-success btn-default" onClick={this.props.handleClick} href="/" data-url={this.props.item.path}>{this.props.item.title}</a></p>
        );
    }
});

var AutoCompleteBox = React.createClass({
    render: function() {
        var nodes = this.props.list.map(function(item){
            return <SoundfileOption soundfile={item} />
        }.bind(this));
        return (
            <div className="autocompleteNodes well category-select hidden">
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
    handleClick: function (e) {
        e.preventDefault();
        playOnServer($(e.target).data('url'));
        $("#search-input").val('').delay(200).trigger('keyup');
        this.handleKeyUp({target: {value:''}});
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
                <AutoCompleteBox handleClick={this.handleClick} list={this.state.autocomplete} />
            </div>
        );
    }
});

var CategorySelectPanel = React.createClass({
    getInitialState: function() {
        return {data: []};
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
    render: function() {
        var categorySelects = this.state.data.map(function (category) {
            return (
                <li className="dropdown">
                    <button className="btn btn-primary btn-lg" type="button" data-toggle="collapse" data-target={'#cat_'+CryptoJS.MD5(category.name)} aria-expanded="false" aria-controls={'#cat_'+CryptoJS.MD5(category.name)}>
                        {category.name} <span className="caret"></span>
                    </button>
                    <CategorySelect category={category} />
                </li>
            );
        });
        return (
            <div className="categorySelectPanel">
                <div className="row">
                    <div className="col-md-3">
                        <div className="col-md-12"><StreamingToggle /></div>
                        <div className="col-md-12"><SoundSearch /></div>
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
