<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>soundboard</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="css/bootstrap.min.readable.css">
    <link rel="stylesheet" href="css/soundboard.css">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Raleway:400,700">
</head>
<body>
<nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container-fluid">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="#">soundboard</a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
                <li class="active"><a href="#">Home</a></li>
                <li><a href="#about">About</a></li>
                <li class="alert-danger" id="kill-button"></li>
            </ul>
        </div><!--/.nav-collapse -->
    </div>
</nav>

<div class="container-fluid">
    <div class="col-md-12">
        <div class="row" id="content"></div>
    </div>
</div><!-- /.container -->

<!-- concat -->
<script src="node_modules/babel-core/browser.min.js"></script>
<script src="bower_components/reconnectingWebsocket/reconnecting-websocket.min.js"></script>
<script src="bower_components/jquery/dist/jquery.min.js"></script>
<script src="bower_components/blueimp-md5/js/md5.min.js"></script>
<script src="bower_components/bootstrap/dist/js/bootstrap.min.js"></script>
<script src="bower_components/react/react-with-addons.min.js"></script>
<script src="bower_components/react/react-dom.min.js"></script>
<!-- /concat -->
<script type="text/babel" src="js/soundboard.js"></script>
</body>
</html>
