<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>soundboard</title>
    <link rel='stylesheet' href='webjars/bootstrap/3.3.5/css/bootstrap.min.css'>
    <link rel='stylesheet' href='css/soundboard.css'>
</head>
<body>
<nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="#">Project name</a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
                <li class="active"><a href="#">Home</a></li>
                <li><a href="#about">About</a></li>
                <li><a href="#contact">Contact</a></li>
            </ul>
        </div><!--/.nav-collapse -->
    </div>
</nav>

<div class="container" id="content"></div><!-- /.container -->

<script src="webjars/jquery/1.11.1/jquery.js"></script>
<script src="webjars/bootstrap/3.3.5/js/bootstrap.min.js"></script>
<script src="webjars/babel/5.8.29-1/browser.min.js"></script>
<script src="webjars/react/0.14.0/react-with-addons.min.js"></script>
<script src="webjars/react/0.14.0/react-dom.min.js"></script>
<script type="text/babel" src="js/soundboard.js"></script>
</body>
</html>
