var gulp = require('gulp');
var gutil = require
var browserify = require('gulp-browserify');
var rename = require('gulp-rename');

gulp.task('default', function() {
  gulp.src(['./static/js/custom.js'])
    .pipe(browserify())
    .pipe(rename('bundle.js'))
    .pipe(gulp.dest('./static/js/'));
});
