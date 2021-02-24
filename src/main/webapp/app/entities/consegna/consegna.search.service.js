(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .factory('ConsegnaSearch', ConsegnaSearch);

    ConsegnaSearch.$inject = ['$resource'];

    function ConsegnaSearch($resource) {
        var resourceUrl =  'api/_search/consegnas/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
