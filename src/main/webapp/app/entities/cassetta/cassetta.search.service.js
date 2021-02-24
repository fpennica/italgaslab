(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .factory('CassettaSearch', CassettaSearch);

    CassettaSearch.$inject = ['$resource'];

    function CassettaSearch($resource) {
        var resourceUrl =  'api/_search/cassettas/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
