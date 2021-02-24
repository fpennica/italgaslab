(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .factory('CodiceIstatSearch', CodiceIstatSearch);

    CodiceIstatSearch.$inject = ['$resource'];

    function CodiceIstatSearch($resource) {
        var resourceUrl =  'api/_search/codice-istats/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
