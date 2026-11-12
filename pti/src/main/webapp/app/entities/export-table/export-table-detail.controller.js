(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ExportTableDetailController', ExportTableDetailController);

    ExportTableDetailController.$inject = ['$scope', '$rootScope', '$uibModalInstance', '$stateParams', 'entity', 'ExportTable', 'PrametreExport', '$uibModal', '$state'];

    function ExportTableDetailController($scope, $rootScope, $uibModalInstance, $stateParams,  entity, ExportTable, PrametreExport, $uibModal, $state) {
        var vm = this;

        vm.exportTable = entity;
        vm.clear = clear;
        vm.addParam = addParam;
        vm.editParam = editParam;
        vm.deleteParam = deleteParam;
        vm.prametreExport = PrametreExport.queryByExportTable({id:vm.exportTable.id});

        
        var unsubscribe = $rootScope.$on('ptiApp:exportTableUpdate', function(event, result) {
            vm.exportTable = result;
        });
        $scope.$on('$destroy', unsubscribe);

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function addParam() {
            $uibModal.open({
                templateUrl: 'app/entities/prametre-export/prametre-export-dialog.html',
                controller: 'PrametreExportDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return {
                            nomParametre: null,
                            exportTable: vm.exportTable,
                            id:null,
                            typeChamps:null,
                            valeur:null,
                            nomChamps:null,
                            tableRef:null,
                            typeListe:null
                            
                        };
                    }
                }
            }).result.then(function() {
                vm.prametreExport = PrametreExport.queryByExportTable({id:vm.exportTable.id});
            }, function() {
                $state.go('^');
            });
        }

        function editParam(idparams){
            console.log("+==========================idparams==================="+idparams);
            $uibModal.open({
                templateUrl: 'app/entities/prametre-export/prametre-export-dialog.html',
                controller: 'PrametreExportDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: ['PrametreExport', function(PrametreExport) {
                        return PrametreExport.get({id : idparams}).$promise;
                    }]
                }
            }).result.then(function() {
                // $state.go('^');
                vm.prametreExport = PrametreExport.queryByExportTable({id:vm.exportTable.id});
            }, function() {
                $state.go('^');
            });
        }
        
        function deleteParam(idparams) {
            $uibModal.open({
                templateUrl: 'app/entities/prametre-export/prametre-export-delete-dialog.html',
                controller: 'PrametreExportDeleteController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    entity: ['PrametreExport', function(PrametreExport) {
                        return PrametreExport.get({id: idparams}).$promise;
                    }]
                }
            }).result.then(function() {
                vm.prametreExport = PrametreExport.queryByExportTable({id:vm.exportTable.id});
            }, function() {
                $state.go('^');
            });
        }
    }
})();
