(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .factory('OperatoreSearch', OperatoreSearch);

    OperatoreSearch.$inject = ['$resource'];

    function OperatoreSearch($resource) {
        var resourceUrl =  'api/_search/operatores/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
