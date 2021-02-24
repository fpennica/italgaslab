(function() {
    'use strict';
    angular
        .module('italgaslabApp')
        .factory('CampioneByCassetta', CampioneByCassetta);

    CampioneByCassetta.$inject = ['$resource', 'DateUtils'];

    function CampioneByCassetta ($resource, DateUtils) {
        var resourceUrl =  'api/cassettas/:id/campiones';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    data.dataAnalisi = DateUtils.convertLocalDateFromServer(data.dataAnalisi);
                    return data;
                }
            }
        });
    }
})();
