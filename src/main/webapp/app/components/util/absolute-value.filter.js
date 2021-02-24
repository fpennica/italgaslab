(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .filter('absoluteValue', absoluteValue);

    function absoluteValue() {
        return function(num) {
            return Math.abs(num);
        };
    }

})();
