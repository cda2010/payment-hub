(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CanalCommunicationDialogController', CanalCommunicationDialogController);

    CanalCommunicationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', '$q', 'entity', 'CanalCommunication', 'Facturier', 'Ftp', 'RecepteurEmail'];

    function CanalCommunicationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, $q, entity, CanalCommunication, Facturier, Ftp, RecepteurEmail) {
        var vm = this;

        vm.canalCommunication = entity;
        vm.clear = clear;
        vm.save = save;
        vm.facturiers = Facturier.query({filter: 'canalcommunication-is-null'});
        $q.all([vm.canalCommunication.$promise, vm.facturiers.$promise]).then(function() {
            if (!vm.canalCommunication.facturier || !vm.canalCommunication.facturier.id) {
                return $q.reject();
            }
            return Facturier.get({id : vm.canalCommunication.facturier.id}).$promise;
        }).then(function(facturier) {
            vm.facturiers.push(facturier);
        });
        vm.ftps = Ftp.query({filter: 'y-is-null'});
        $q.all([vm.canalCommunication.$promise, vm.ftps.$promise]).then(function() {
            if (!vm.canalCommunication.ftp || !vm.canalCommunication.ftp.id) {
                return $q.reject();
            }
            return Ftp.get({id : vm.canalCommunication.ftp.id}).$promise;
        }).then(function(ftp) {
            vm.ftps.push(ftp);
        });
        vm.recepteuremails = RecepteurEmail.query({filter: 'canalcommunication-is-null'});
        $q.all([vm.canalCommunication.$promise, vm.recepteuremails.$promise]).then(function() {
            if (!vm.canalCommunication.recepteurEmail || !vm.canalCommunication.recepteurEmail.id) {
                return $q.reject();
            }
            return RecepteurEmail.get({id : vm.canalCommunication.recepteurEmail.id}).$promise;
        }).then(function(recepteurEmail) {
            vm.recepteuremails.push(recepteurEmail);
        });

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.canalCommunication.id !== null) {
                CanalCommunication.update(vm.canalCommunication, onSaveSuccess, onSaveError);
            } else {
                CanalCommunication.save(vm.canalCommunication, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:canalCommunicationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
