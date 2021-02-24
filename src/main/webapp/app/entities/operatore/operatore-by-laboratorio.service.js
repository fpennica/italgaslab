(function() {
    'use strict';
    angular
        .module('italgaslabApp')
        .factory('OperatoreByLaboratorio', OperatoreByLaboratorio);

    OperatoreByLaboratorio.$inject = ['$resource'];

    function OperatoreByLaboratorio ($resource) {
        var resourceUrl =  'api/laboratorios/:id/operatores';

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
            }
        });
    }
})();
