(function() {
    'use strict';
    angular
        .module('italgaslabApp')
        .factory('Operatore', Operatore);

    Operatore.$inject = ['$resource'];

    function Operatore ($resource) {
        var resourceUrl =  'api/operatores/:id';

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
