const gulp = require('gulp');
const browserify = require('gulp-browserify');
const rename = require('gulp-rename');
const shell = require('gulp-shell');

gulp.task('default', function() {
  gulp.src(['./js/custom.js']).
      pipe(browserify()).
      pipe(rename('bundle.js')).
      pipe(gulp.dest('./js'));
});

gulp.task('protoc', () => {
  return gulp.src('').pipe(shell([
    '/opt/protoc-3.0/bin/protoc --js_out=import_style=commonjs,binary:js/ proto/Gym.proto',
  ]));
});
