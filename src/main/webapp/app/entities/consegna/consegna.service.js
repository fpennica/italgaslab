(function() {
    'use strict';
    angular
        .module('italgaslabApp')
        .factory('Consegna', Consegna);

    Consegna.$inject = ['$resource', 'DateUtils'];

    function Consegna ($resource, DateUtils) {
        var resourceUrl =  'api/consegnas/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dataConsegna = DateUtils.convertLocalDateFromServer(data.dataConsegna);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    data.dataConsegna = DateUtils.convertLocalDateToServer(data.dataConsegna);
                    return angular.toJson(data);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    data.dataConsegna = DateUtils.convertLocalDateToServer(data.dataConsegna);
                    return angular.toJson(data);
                }
            }
        });
    }
})();
