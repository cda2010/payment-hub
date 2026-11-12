(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeServiceReseauOrganisationDialogController', TypeServiceReseauOrganisationDialogController);

        TypeServiceReseauOrganisationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity','UniteOrganisation', 'TypeServiceReseauOrganisation','TypeService'];

    function TypeServiceReseauOrganisationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity,UniteOrganisation, TypeServiceReseauOrganisation,TypeService) {
        var vm = this;

        vm.typeServiceReseauOrganisation = entity;
        vm.clear = clear;     
        vm.save = save;
        vm.typeservices=TypeService.queryAll();
        vm.onChangeCodePays = onChangeCodePays;
        

        function onChangeCodePays () {
            console.log("codeindicatif+==============="+vm.typeServiceReseauOrganisation.codePays)
            UniteOrganisation.queryByTypeUoFSEAndCodeIndicatif({codePays: vm.typeServiceReseauOrganisation.codePays}, function (datas) {
            vm.FSEs= datas;
            });
        }
        $timeout(function (){  
            angular.element('.form-group:eq(1)>input').focus();
        }); 

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }
     
        function save () {
            vm.isSaving = true;   
            if (vm.typeServiceReseauOrganisation.id !== null) {
                TypeServiceReseauOrganisation.update(vm.typeServiceReseauOrganisation, onSaveSuccess, onSaveError);
            } else {
                TypeServiceReseauOrganisation.save(vm.typeServiceReseauOrganisation, onSaveSuccess, onSaveError);
            }
        }
        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:paramCanalUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

    }
})();
