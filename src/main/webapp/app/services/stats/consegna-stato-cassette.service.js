(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .factory('ConsegnaStatoCassette', ConsegnaStatoCassette);

    ConsegnaStatoCassette.$inject = ['$resource'];

    function ConsegnaStatoCassette($resource) {

        var resourceUrl =  'api/stats/consegna/:id/cassette';

        return $resource(resourceUrl, {}, {
            'get': {
                method: 'GET',
                isArray: false
                
            }
        });

    }


})();
