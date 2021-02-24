(function() {
    'use strict';
    angular
        .module('italgaslabApp')
        .factory('Cassetta', Cassetta);

    Cassetta.$inject = ['$resource', 'DateUtils'];

    function Cassetta ($resource, DateUtils) {
        var resourceUrl =  'api/cassettas/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dataScavo = DateUtils.convertLocalDateFromServer(data.dataScavo);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    data.dataScavo = DateUtils.convertLocalDateToServer(data.dataScavo);
                    return angular.toJson(data);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    data.dataScavo = DateUtils.convertLocalDateToServer(data.dataScavo);
                    return angular.toJson(data);
                }
            }
        });
    }
})();
