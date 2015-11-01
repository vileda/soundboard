'use strict';

module.exports = function(grunt) {

    require('load-grunt-tasks')(grunt);
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-replace');

    // Project configuration.
    grunt.initConfig({
        "babel": {
            options: {
                sourceMap: true
            },
            dist: {
                files: {
                    "dist/js/soundboard.js": "src/main/webapp/js/soundboard.js"
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
                            match: /type="text.babel" /,
                            replacement: ''
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
                    {expand: true, flatten: true, src: ['dist/js/**'], dest: 'target/soundboard/js'}
                ]
            }
        }
    });

    // Default task(s).
    grunt.registerTask('default', ['babel', 'copy', 'replace']);

};