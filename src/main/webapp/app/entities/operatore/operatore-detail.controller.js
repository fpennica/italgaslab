(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('OperatoreDetailController', OperatoreDetailController);

    OperatoreDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'entity', 'Operatore', 'User', 'Laboratorio'];

    function OperatoreDetailController($scope, $rootScope, $stateParams, entity, Operatore, User, Laboratorio) {
        var vm = this;

        vm.operatore = entity;

        var unsubscribe = $rootScope.$on('italgaslabApp:operatoreUpdate', function(event, result) {
            vm.operatore = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
