(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .factory('PesataSearch', PesataSearch);

    PesataSearch.$inject = ['$resource'];

    function PesataSearch($resource) {
        var resourceUrl =  'api/_search/pesatas/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
