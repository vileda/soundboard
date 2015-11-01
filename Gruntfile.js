'use strict';

module.exports = function(grunt) {

    require('load-grunt-tasks')(grunt);
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-replace');
    grunt.loadNpmTasks('grunt-contrib-cssmin');
    grunt.loadNpmTasks('grunt-bower-concat');
    grunt.loadNpmTasks('grunt-contrib-uglify');

    // Project configuration.
    grunt.initConfig({
        "babel": {
            options: {
                sourceMap: false
            },
            dist: {
                files: {
                    "dist/js/soundboard.js": "src/main/webapp/js/soundboard.js"
                }
            }
        },
        bower_concat: {
            all: {
                dest: 'dist/js/libs.js',
                cssDest: 'dist/css/soundboard.min.css',
                exclude: [],
                dependencies: {
                    'jquery': 'reconnectingWebsocket',
                    'bootstrap': 'jquery',
                    'react-dom': 'react',
                    'react': 'jquery',
                    'crypto-js': 'jquery'
                },
                bowerOptions: {
                    relative: false
                }
            }
        },
        uglify: {
            options: {
                mangle: false
            },
            soundboard: {
                files: {
                    'dist/js/libs.min.js': ['dist/js/libs.js'],
                    'dist/js/soundboard.min.js': ['dist/js/soundboard.js']
                }
            }
        },
        replace: {
            dist: {
                options: {
                    patterns: [
                        {
                            match: /<script src="webjars\/babel\/.*\/.*"><\/script>/,
                            replacement: ''
                        },{
                            match: /<link rel="stylesheet" href="css\/bootstrap\.min\.readable\.css">/,
                            replacement: ''
                        },{
                            match: /<link rel="stylesheet" href="css\/soundboard.css">/,
                            replacement: '<link rel="stylesheet" href="css/soundboard.min.css">'
                        },{
                            match: /<!-- concat -->[\s\S]*<!-- \/concat -->/gm,
                            replacement: '<script src="js/libs.min.js"></script>'
                        },{
                            match: /<script type="text\/babel" src="js\/soundboard.js"><\/script>/,
                            replacement: '<script src="js/soundboard.min.js"></script>'
                        }
                    ]
                },
                files: [
                    {expand: true, flatten: true, src: ['src/main/webapp/index.jsp'], dest: 'dist/'}
                ]
            }
        },
        copy: {
            main: {
                files: [
                    {expand: true, flatten: true, src: ['dist/*.jsp'], dest: 'target/soundboard'},
                    {expand: true, flatten: true, src: ['dist/js/*.min.js'], dest: 'target/soundboard/js'},
                    {expand: true, flatten: true, src: ['dist/css/soundboard.min.css'], dest: 'target/soundboard/css'}
                ]
            }
        }
    });

    // Default task(s).
    grunt.registerTask('default', ['babel', 'bower_concat', 'uglify', 'copy']);

};