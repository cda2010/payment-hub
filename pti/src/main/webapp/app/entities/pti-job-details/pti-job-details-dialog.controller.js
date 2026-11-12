(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PtiJobDetailsDialogController', PtiJobDetailsDialogController);

    PtiJobDetailsDialogController.$inject = ['TableService','$timeout','isViewing', '$scope', '$stateParams', '$uibModalInstance', '$uibModal','entity', 'PtiJobDetails', 'HistExecutionJob','Notification','FournisseurService','ParamExecutionJob'];

    function PtiJobDetailsDialogController (TableService,$timeout,isViewing, $scope, $stateParams, $uibModalInstance, $uibModal,entity, PtiJobDetails, HistExecutionJob,Notification,FournisseurService,ParamExecutionJob) {
        var vm = this;
        vm.isViewing=isViewing;
        vm.ptiJobDetails = entity;
        console.log('look job',vm.ptiJobDetails);
         vm.paramExecutionJobs = [];
        vm.clearJob = clearJob;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.addParametre=addParametre;
        vm.editParametre=editParametre;
        vm.deleteParametre=deleteParametre;
        vm.detailParametre=detailParametre;
        vm.paramExecutionJobs=ParamExecutionJob.queryByPtiJobDetail({id:vm.ptiJobDetails.id});
       // vm.histexecutionjobs = HistExecutionJob.query();
        FournisseurService.find(function (result) {
            vm.fournisseurServices=result;
            console.log(result);
        });
       
        init();
         
        
        function load () {
             ParamExecutionJob.query(function(result) {
                vm.paramExecutionJobs = result;
               
            });
        }
        
        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clearJob () {
            console.log('ok0');
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            console.log(vm.ptiJobDetails);
            vm.isSaving = true;
            if (vm.ptiJobDetails.id !== null) {
                PtiJobDetails.update(vm.ptiJobDetails, onSaveSuccess, onSaveError);
            } else {
                PtiJobDetails.save(vm.ptiJobDetails, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:ptiJobDetailsUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError (error) {
            vm.isSaving = false;
            Notification.error(error.data.message);
        }

        vm.datePickerOpenStatus.dateDerniereExecution = false;
        vm.datePickerOpenStatus.dateProchaineExecution = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }

        function deleteParametre(idparametre) {
            $uibModal.open({
                templateUrl: 'app/entities/param-execution-job/param-execution-job-delete-dialog.html',
                controller: 'ParamExecutionJobDeleteController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    entity: ['ParamExecutionJob', function(ParamExecutionJob) {
                        return ParamExecutionJob.get({id : idparametre}).$promise;
                    }]
                }
            }).result.then(function() {
                vm.paramExecutionJobs=ParamExecutionJob.queryByPtiJobDetail({id:vm.ptiJobDetails.id});
                Notification.success("Paramètre supprimé avec succès");
            }, function() {

            });
        }

        function addParametre() {
            console.log('moi');
            $uibModal.open({
                templateUrl: 'app/entities/param-execution-job/param-execution-job-dialog.html',
                controller: 'ParamExecutionJobDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return {
                            cle: null,
                            valeur: null,
                            id: null,
                            ptiJobDetail: vm.ptiJobDetails
                        };
                    }
                }
            }).result.then(function() {
                vm.paramExecutionJobs=ParamExecutionJob.queryByPtiJobDetail({id:vm.ptiJobDetails.id});
                Notification.success("Paramètre enregistré  avec succès");
            }, function() {

            });
        }

        function editParametre(idparametre) {
            $uibModal.open({
                templateUrl: 'app/entities/param-execution-job/param-execution-job-dialog.html',
                controller: 'ParamExecutionJobDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: ['ParamExecutionJob', function(ParamExecutionJob) {
                        return ParamExecutionJob.get({id :idparametre}).$promise;
                    }]
                }
            }).result.then(function() {
                vm.paramExecutionJobs=ParamExecutionJob.queryByPtiJobDetail({id:vm.ptiJobDetails.id});
                Notification.success("Parametre modifié avec succès");
            }, function() {

            });
        }

        function detailParametre(idparametre) {
            $uibModal.open({
                templateUrl: 'app/entities/param-execution-job/param-execution-job-detail.html',
                controller: 'ParamExecutionJobDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: ['ParamExecutionJob', function(ParamExecutionJob) {
                        return ParamExecutionJob.get({id :idparametre}).$promise;
                    }]
                }
            }).result.then(function() {
                vm.paramExecutionJobs=ParamExecutionJob.queryByPtiJobDetail({id:vm.ptiJobDetails.id});
                Notification.success("Parametre modifié avec succès");
            }, function() {

            });
        }



         function init() {
            var addItemData = {
                templateUrl: 'app/entities/param-execution-job/param-execution-job-dialog.html',
                controller: 'ParamExecutionJobDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: function () {
                        return {
                            /*Initialisation des valeurs par défauts de l'entité à créer*/
                            cle: null,
                            valeur: null,
                            id: null
                        };
                    }
                }
            };
             var editItemData = {
                 templateUrl: 'app/entities/param-execution-job/param-execution-job-dialog.html',
                 controller: 'ParamExecutionJobDialogController',
                 controllerAs: 'vm',
                 backdrop: 'static',
                 size: 'lg',
                 resolve: {
                     isViewing: function () {
                         return false;
                     },
                     isEdit: function () {
                         return true;
                     },
                     entity: null //valeur déterminé à l'appel de la boite
                 }
             };



             var detailItemData = {
               templateUrl: 'app/entities/param-execution-job/param-execution-job-dialog.html',
                    controller: 'ParamExecutionJobDialogController',
                controllerAs: 'vm',
                size: 'lg',
                resolve: {
                    isViewing: function () {
                        return true;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: null //valeur déterminé à l'appel de la boite,
                }
            };
            var deleteItemData = {
                templateUrl: 'app/entities/param-execution-job/param-execution-job-delete-dialog.html',
                    controller: 'ParamExecutionJobDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };
            var dialogData = {
                addData: addItemData,
                editData: editItemData,
                detailData: detailItemData,
                deleteData: deleteItemData
            };
            var entitySearchParams = function () {
                return {};
            };
            var entityParams = function () {
                return {
                    
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: ParamExecutionJob,
                dialogData: dialogData,
                entitySearch: ParamExecutionJob,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams,
                loadCallBack: vm.load
            };
            TableService.init(datas);

        }
    }
})();
