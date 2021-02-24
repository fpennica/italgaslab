(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .factory('CassettaSearchByConsegna', CassettaSearchByConsegna);

    CassettaSearchByConsegna.$inject = ['$resource'];

    function CassettaSearchByConsegna($resource) {
        var resourceUrl =  'api/_search/consegnas/:idConsegna/cassettas/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
