(function() {
    'use strict';
    angular
        .module('italgaslabApp')
        .factory('Laboratorio', Laboratorio);

    Laboratorio.$inject = ['$resource'];

    function Laboratorio ($resource) {
        var resourceUrl =  'api/laboratorios/:id';

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
