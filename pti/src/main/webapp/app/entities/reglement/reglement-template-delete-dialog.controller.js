(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ReglementTemplateDeleteController',ReglementTemplateDeleteController);

    ReglementTemplateDeleteController.$inject = ['$uibModalInstance', 'entity', 'ReglementTemplate','$rootScope'];

    function ReglementTemplateDeleteController($uibModalInstance, entity, ReglementTemplate,$rootScope) {
        var vm = this;

       
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
         vm.reglementTemplate = entity;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }
        function confirmDelete () {
          console.log(vm.reglementTemplate);
            ReglementTemplate.delete({id: vm.reglementTemplate.id},
                function () {
                   $rootScope.$emit('ptiApp:majReglementTremplate');
                    $uibModalInstance.close(true);
                });
        }
    }
})();
