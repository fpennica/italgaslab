(function() {
    'use strict';

    angular
        .module('italgaslabApp')
        .controller('CodiceIstatDeleteController',CodiceIstatDeleteController);

    CodiceIstatDeleteController.$inject = ['$uibModalInstance', 'entity', 'CodiceIstat'];

    function CodiceIstatDeleteController($uibModalInstance, entity, CodiceIstat) {
        var vm = this;

        vm.codiceIstat = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            CodiceIstat.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
