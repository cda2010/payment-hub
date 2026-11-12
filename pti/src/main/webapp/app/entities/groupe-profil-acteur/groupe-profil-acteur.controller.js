(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('GroupeProfilActeurController', GroupeProfilActeurController);

    GroupeProfilActeurController.$inject = ['$state','$scope','$uibModal', 'GroupeProfilActeur', 'ProfilActeur','Notification','TableService'];

    function GroupeProfilActeurController($state,$scope,$uibModal, GroupeProfilActeur, ProfilActeur,Notification,TableService) {

        var vm = this;
        vm.init=init;
        vm.curentGroup=null;
        vm.profils=null;
        vm.findProfils=function(groupe){
            vm.curentGroup=groupe;
            ProfilActeur.queryByGroupe({groupeId:groupe.id},function (result) {
                vm.profils=result;
            })
        };
        vm.detailProfil=function(profil){
            $uibModal.open({
                templateUrl: 'app/entities/profil-acteur/profil-acteur-dialog.html',
                controller: 'ProfilActeurDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return profil
                    },
                    isViewing:true,
                }
            }).result.then(function() {
                //vm.findTableauRepartitions(vm.curentGroup);
                //Notification.success("Profil Modifier avec succès");
            }, function() {
            });
        }
        vm.editProfil=function(profil){
                $uibModal.open({
                    templateUrl: 'app/entities/profil-acteur/profil-acteur-dialog.html',
                    controller: 'ProfilActeurDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return profil
                        },
                        isViewing:false,
                    }
                }).result.then(function() {
                    //vm.findTableauRepartitions(vm.curentGroup);
                    Notification.success("Profil Modifier avec succès");
                    vm.findProfils(vm.curentGroup);
                }, function() {
                });
        }

        init();
        function init(){
            var addItemData = {
                templateUrl: 'app/entities/groupe-profil-acteur/groupe-profil-acteur-dialog.html',
                controller: 'GroupeProfilActeurDialogController',
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
                            nom: null,
                            description: null,
                            type: null,
                            destineA: null,
                            codeFSE: null,
                            active: null,
                            id: null
                        };
                    }
                }
            };

            var editItemData = {
                templateUrl: 'app/entities/groupe-profil-acteur/groupe-profil-acteur-dialog.html',
                controller: 'GroupeProfilActeurDialogController',
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
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };

            var detailItemData = {
                templateUrl: 'app/entities/groupe-profil-acteur/groupe-profil-acteur-dialog.html',
                controller: 'GroupeProfilActeurDialogController',
                controllerAs: 'vm',
                size: 'md',
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
                templateUrl: 'app/entities/groupe-profil-acteur/groupe-profil-acteur-delete-dialog.html',
                controller: 'GroupeProfilActeurDeleteController',
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
                entity: GroupeProfilActeur,
                dialogData: dialogData,
                entitySearch: GroupeProfilActeur,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams,
                loadCallBack: vm.load
            };
            TableService.init(datas);

        }

    }
})();
