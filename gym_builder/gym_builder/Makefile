SRC_DIR=../../climb/app/src/main/proto/com/peter/climb
PROTO=$(SRC_DIR)/Gym.proto

browserify:
	browserify static/js/custom.js > static/js/bundle.js

protoc:
	/opt/protoc-3.0/bin/protoc -I=$(SRC_DIR) --python_out=. $(PROTO)
	/opt/protoc-3.0/bin/protoc --proto_path=$(SRC_DIR) --js_out=import_style=commonjs,binary:static/js/ $(PROTO)

all:
	protoc
	browserify


