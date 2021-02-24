(function() {
    'use strict';
    angular
        .module('italgaslabApp')
        .factory('CodiceIstat', CodiceIstat);

    CodiceIstat.$inject = ['$resource'];

    function CodiceIstat ($resource) {
        var resourceUrl =  'api/codice-istats/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
