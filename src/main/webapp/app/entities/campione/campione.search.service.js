(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .factory('CampioneSearch', CampioneSearch);

    CampioneSearch.$inject = ['$resource'];

    function CampioneSearch($resource) {
        var resourceUrl =  'api/_search/campiones/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
