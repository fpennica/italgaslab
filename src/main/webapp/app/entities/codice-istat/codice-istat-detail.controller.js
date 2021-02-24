(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('CodiceIstatDetailController', CodiceIstatDetailController);

    CodiceIstatDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'entity', 'CodiceIstat'];

    function CodiceIstatDetailController($scope, $rootScope, $stateParams, entity, CodiceIstat) {
        var vm = this;

        vm.codiceIstat = entity;

        var unsubscribe = $rootScope.$on('italgaslabApp:codiceIstatUpdate', function(event, result) {
            vm.codiceIstat = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
