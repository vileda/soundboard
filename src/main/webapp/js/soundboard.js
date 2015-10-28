
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
                <li><CategorySelect category={category} /></li>
            );
        });
        return (
            <div className="categorySelectPanel">
                <ul>{categorySelects}</ul>
            </div>
        );
    }
});

ReactDOM.render(
    <CategorySelectPanel url="/soundboard/sounds" />,
    document.getElementById('content')
);