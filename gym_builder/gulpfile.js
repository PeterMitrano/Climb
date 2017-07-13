let gulp = require('gulp');
let browserify = require('gulp-browserify');
let rename = require('gulp-rename');

gulp.task('default', function() {
  gulp.src(['./static/js/custom.js']).
      pipe(browserify()).
      pipe(rename('bundle.js')).
      pipe(gulp.dest('./static/js/'));
});
