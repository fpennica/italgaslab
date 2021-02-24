(function() {
    'use strict';
    angular
        .module('italgaslabApp')
        .factory('PesataByCampione', PesataByCampione);

    PesataByCampione.$inject = ['$resource'];

    function PesataByCampione ($resource) {
        var resourceUrl =  'api/campiones/:id/pesatas';

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
            'queryWithChart': {
                method: 'GET',
                isArray: true,
                url: 'api/campiones/:id/pesatasWithChart'
            },
            'queryWithChartUsura': {
                method: 'GET',
                params: {cb: true, binder: false},
                isArray: true,
                url: 'api/campiones/:id/pesatasWithChart'
            },
            'queryWithChartBinder': {
                method: 'GET',
                params: {cb: true, binder: true},
                isArray: true,
                url: 'api/campiones/:id/pesatasWithChart'
            },
            'queryBinder': {
                method: 'GET',
                params: {binder: true},
                isArray: true
            },
            'queryUsura': {
                method: 'GET',
                params: {binder: false},
                isArray: true
            }
        });
    }
})();
