(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EvenementOperationController', EvenementOperationController);

    EvenementOperationController.$inject = ['$scope', '$state','InitCrud', '$uibModal','EvenementOperation','CancelAlert','RequestUtils','Notification','TableService', '$translate'];

    function EvenementOperationController($scope, $state,InitCrud, $uibModal,EvenementOperation,CancelAlert,RequestUtils,Notification,TableService, $translate) {

        var vm = this;
        vm.title = $translate.instant('ptiApp.operation.listeEvenementOp');

        //Construction des champs du formulaire pour le filtre
        vm.setActive = setActive;
        vm.filtrer = false;
        vm.addEvenement=addEvenement;
        vm.itemSearch = {
            "telephone": null
        };

        vm.initFiltre = function () {
            vm.filtrer = !vm.filtrer;
            if (!vm.filtrer) {
                vm.itemSearch = {
                    "telephone": null
                };

                init();
            }
        };
        vm.filtre = function () {
            console.log("filtre des informations en cours",vm.itemSearch.telephone);
            init();
        };

        function setActive(isActivated,evenementOperation){
            console.log('oo',isActivated);
            vm.operation = {
                "commentaire": "Macker : "+evenementOperation.motif,
                "id": evenementOperation.id,
                "estLevee":isActivated
            };
            $uibModal.open({
                templateUrl: 'app/entities/operation/account-cash/account-cash-traitement.html',
                controller: 'EvenementTraitementController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return vm.operation;
                    }
                }
            }).result.then(function () {
                Notification.success("Opération effectuée avec succès.");
                //$uibModalInstance.close(true);
                //   $state.reload();
                //$state.go('operation',{}, {reload: true});
            });
        }



        init();

        function init() {
            console.log("filtre en cours");
            var addItemData = {
                templateUrl: 'app/entities/evenement-operation/evenement-operation-dialog.html',
                controller: 'EvenementOperationDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: function () {
                        return {
                            login: null,
                            email: null,
                            id: null
                        };
                    }
                }
            };

            /**
             *Modification d'un élément
             */
            var editItemData = {
                templateUrl: 'app/entities/evenement-operation/evenement-operation-dialog.html',
                controller: 'EvenementOperationDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return true;
                    },
                    entity: null
                }
            };

            /**
             * Suppresion d'un élément
             */
            var deleteItemData = {
                templateUrl: 'app/entities/evenement-operation/entities/evenement-operation-delete-dialog.html',
                controller: 'EvenementOperationDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null
                }
            };

            var detailItemData = {
                templateUrl: 'app/entities/evenement-operation/evenement-operation-dialog.html',
                controller: 'EvenementOperationDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return true;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };

            var dialogData = {
                addData: addItemData,
                editData: editItemData,
                deleteData: deleteItemData,
                detailData: detailItemData
            };
            var entitySearchParams = function () {
                return {};
            };

            var entityParams = function () {
                return {
                    "telephone": vm.itemSearch.telephone
                };
            };

            var datas = {
                scope: $scope,
                vm: vm,
                entity: EvenementOperation,
                entitySearch: EvenementOperation,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
            console.log('event',datas);
        }

        function addEvenement() {
            $uibModal.open({
                templateUrl: 'app/entities/evenement-operation/init-operation.html',
                controller: 'InitOperationController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return{
                            montantOp: 0,
                            montantFrais: null,
                            dateOperation: null,
                            statut: null,
                            codeRetrait: null,
                            motif:null,
                            id: null
                        }
                    },
                    isViewing: function () {
                        return false;
                    },
                    isExpired: function () {
                        return false;
                    },
                    isMaj: function () {
                        return false;
                    },
                    isChange: function () {
                        return false;
                    },
                }
            }).result.then(function() {
                console.log('Operation refresh');

            }, function() {
            });

        }
    }
})();
