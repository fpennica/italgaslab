(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .factory('Certificato', Certificato);

    Certificato.$inject = ['$q', '$timeout', '$window', '$resource'];

    function Certificato($q, $timeout, $window, $resource) {

        var resourceUrl =  'api/cassettas/:idCassetta/cert';

        return $resource(resourceUrl, {}, {
            'get': {
                method: 'GET',
                params: {model: false},
                interceptor: {
                    response: function(response) {
                        // expose response
                        return response;
                    }
                }
            }
        });

    }


})();
