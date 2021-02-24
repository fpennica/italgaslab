(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('OperatoreDeleteController',OperatoreDeleteController);

    OperatoreDeleteController.$inject = ['$uibModalInstance', 'entity', 'Operatore'];

    function OperatoreDeleteController($uibModalInstance, entity, Operatore) {
        var vm = this;

        vm.operatore = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Operatore.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
