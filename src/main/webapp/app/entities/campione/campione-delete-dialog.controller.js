(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('CampioneDeleteController',CampioneDeleteController);

    CampioneDeleteController.$inject = ['$uibModalInstance', 'entity', 'Campione'];

    function CampioneDeleteController($uibModalInstance, entity, Campione) {
        var vm = this;

        vm.campione = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Campione.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
