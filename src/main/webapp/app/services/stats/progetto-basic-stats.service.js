(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .factory('ProgettoBasicStats', ProgettoBasicStats);

    ProgettoBasicStats.$inject = ['$resource'];

    function ProgettoBasicStats($resource) {

        var resourceUrl =  'api/stats/progetto/basicstats';

        return $resource(resourceUrl, {}, {
            'get': {
                method: 'GET',
                isArray: false
                
            }
        });

    }


})();
