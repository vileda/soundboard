
var SoundfileOption = React.createClass({
    render: function() {
        return (
            <option onClick={this.onClick} value={this.props.soundfile.path}>{this.props.soundfile.title}</option>
        );
    }
});

var CategorySelect = React.createClass({
    onChange: function(e) {
        var path = e.target.value;
        $.ajax({
            url: 'sounds',
            contentType: 'application/json',
            method: 'POST',
            data: path
        });
        e.target.value = 0;
    },
    render: function() {
        var soundfileOptions = this.props.category.soundfiles.map(function (soundfile) {
            return (
                <SoundfileOption soundfile={soundfile} />
            );
        });
        return (
            <select onChange={this.onChange} className="categorySelect">
                <option value="0">{this.props.category.name}</option>
                {soundfileOptions}
            </select>
        );
    }
});

var AutoComplete = React.createClass({
    render: function() {
        return (
            <p><a onClick={this.props.handleClick} href="/" data-url={this.props.item.path}>{this.props.item.title}</a></p>
        );
    }
});

/**
 * this component is the parent of AutoComplete
 */
var AutoCompleteBox = React.createClass({
    render: function() {
        //note: we are producing a new immutable array here
        var nodes = this.props.list.map(function(item){
            return <AutoComplete handleClick={this.props.handleClick} item={item} />;
        }.bind(this));
        return (
            <div className="autocompleteNodes">
                {nodes}
            </div>
        );
    }
});

var RemotePlay = React.createClass({
   handleKeyUp: function(e) {
       console.log(e);
       if(e.keyCode === 13) {
           $.ajax({
               url: 'sounds',
               contentType: 'application/json',
               method: 'POST',
               data: $(e.target).val()
           });
           e.target.value = '';
       }
   },
   render: function () {
       return (
           <label>Remote:
            <input onKeyUp={this.handleKeyUp} type="text" placeholder="URL" />
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
        $.ajax({
            url: 'sounds',
            contentType: 'application/json',
            method: 'POST',
            data: $(e.target).data('url')
        });
        $("#search-input").val('').delay(200).trigger('keyup');
        this.handleKeyUp({target: {value:''}});
    },
    makeCall: function(term, current) {
        var searchUrl = "/sounds/search?q="+encodeURIComponent(term);
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
        if (k.length > 1 ) {
            var priority = this.state.call.latest+1;
            this.setState({call: {latest: priority, term: k }});
        }
        if (k.length == 0 && this.state.autocomplete.length > 0 ) {
            this.setState({autocomplete: [], call: {latest:0, term:''}});
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
                <span>Sound:</span>
                <input id="search-input" type="text" placeholder="search" onKeyUp={this.handleKeyUp} />
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
    handleClick: function() {
      $.ajax({
          url: 'sounds/kill',
          method: 'POST',
          contentType: 'application/json'
      });
    },
    render: function() {
        var categorySelects = this.state.data.map(function (category) {
            return (
                <li><CategorySelect category={category} /></li>
            );
        });
        return (
            <div className="categorySelectPanel">
                <button onClick={this.handleClick} type="button">Kill</button>
                <SoundSearch />
                <RemotePlay />
                <ul>{categorySelects}</ul>
            </div>
        );
    }
});

ReactDOM.render(
    <CategorySelectPanel url="/sounds" />,
    document.getElementById('content')
);
