(function() {
    'use strict';
    angular
        .module('italgaslabApp')
        .factory('CassettaByConsegna', CassettaByConsegna);

    CassettaByConsegna.$inject = ['$resource', 'DateUtils'];

    function CassettaByConsegna ($resource, DateUtils) {
        var resourceUrl =  'api/consegnas/:idConsegna/cassettas';

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
            }
        });
    }
})();
