(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .factory('LaboratorioStatoCassette', LaboratorioStatoCassette);

    LaboratorioStatoCassette.$inject = ['$resource'];

    function LaboratorioStatoCassette($resource) {

        var resourceUrl =  'api/stats/laboratorio/cassette';

        return $resource(resourceUrl, {}, {
            'get': {
                method: 'GET',
                isArray: false
                
            }
        });

    }


})();
