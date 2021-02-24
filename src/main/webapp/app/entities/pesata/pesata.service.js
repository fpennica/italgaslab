(function() {
    'use strict';
    angular
        .module('italgaslabApp')
        .factory('Pesata', Pesata);

    Pesata.$inject = ['$resource'];

    function Pesata ($resource) {
        var resourceUrl =  'api/pesatas/:id';

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
