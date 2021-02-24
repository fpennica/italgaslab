(function() {
    'use strict';
    angular
        .module('italgaslabApp')
        .factory('Campione', Campione);

    Campione.$inject = ['$resource', 'DateUtils'];

    function Campione ($resource, DateUtils) {
        var resourceUrl =  'api/campiones/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dataAnalisi = DateUtils.convertLocalDateFromServer(data.dataAnalisi);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    data.dataAnalisi = DateUtils.convertLocalDateToServer(data.dataAnalisi);
                    return angular.toJson(data);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    data.dataAnalisi = DateUtils.convertLocalDateToServer(data.dataAnalisi);
                    return angular.toJson(data);
                }
            },
            'getClassGeotec': {
                method: 'GET',
                isArray: false,
                url: 'api/campiones/:id/classgeotec'
            },
            'getCbUsuraPercGran': {
                method: 'GET',
                isArray: false,
                url: 'api/campiones/:id/cbusurapercgran'
            },
            'getCbBinderPercGran': {
                method: 'GET',
                isArray: false,
                url: 'api/campiones/:id/cbbinderpercgran'
            }
        });
    }
})();
