(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('PesataDetailController', PesataDetailController);

    PesataDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'entity', 'Pesata', 'Campione'];

    function PesataDetailController($scope, $rootScope, $stateParams, entity, Pesata, Campione) {
        var vm = this;

        vm.pesata = entity;

        var unsubscribe = $rootScope.$on('italgaslabApp:pesataUpdate', function(event, result) {
            vm.pesata = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
