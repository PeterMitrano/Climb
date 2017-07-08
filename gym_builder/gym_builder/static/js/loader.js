// Original work Copyright @frostney 2015
// https://gist.github.com/frostney/9cdc735cc1e26487328e

(function(root) {
	
	var modules = {};
	var cache = {};

	root.require = function(name) {
		if (!Object.hasOwnProperty.call(cache, name)) {
			var module = {
				id: name,
			};
			var exports = {};
			var require = root.require;

			modules[name].call(module, require, module, exports);

			cache[name] = hasOwn.call(module, 'exports') ? module.exports : exports;
		}

		return cache[name];
	};

	// factory (require, module, exports)
	root.require.register = function(name, factory) {
		if (!factory) {
			throw new TypeError('expected factory function or object');
		}
		if (typeof factory === 'object') {
			modules[name] = function(require, module) {
				module.exports = factory;
			};
		} else {
			modules[name] = factory;
		}
	};

})(window);
