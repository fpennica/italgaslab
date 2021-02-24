(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('ConsegnaDeleteController',ConsegnaDeleteController);

    ConsegnaDeleteController.$inject = ['$uibModalInstance', 'entity', 'Consegna'];

    function ConsegnaDeleteController($uibModalInstance, entity, Consegna) {
        var vm = this;

        vm.consegna = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Consegna.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
