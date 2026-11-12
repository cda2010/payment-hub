(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PointVenteController', PointVenteController);

    PointVenteController.$inject = ['RequestUtils','$uibModal','PointVente', 'TableService', '$scope'];

    function PointVenteController(RequestUtils,$uibModal,PointVente, TableService, $scope) {

        var vm = this;


        var addItemData = {
            templateUrl: 'app/entities/point-vente/point-vente-dialog.html',
            controller: 'PointVenteDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                isViewing: function() {
                    return false;
                },
                isSaveInUO: function() {
                    return true;
                },
                entity: function() {
                    return {
                        /*Initialisation des valeurs par défauts de l'entité à créer*/
                        id: null,
                        codePointVente: null,
                        libellePointVente: null,
                        uniteOrganisationId: null

                    };
                }
            }
        };

        var editItemData = {
            templateUrl: 'app/entities/point-vente/point-vente-dialog.html',
            controller: 'PointVenteDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                isViewing: function() {
                    return false;
                },
                entity: null //valeur déterminé à l'appel de la boite
            }
        };

        var detailItemData = {
            templateUrl: 'app/entities/point-vente/point-vente-dialog.html',
            controller: 'PointVenteDialogController',
            controllerAs: 'vm',
            size: 'md',
            resolve: {
                isViewing: function() {
                    return true;
                },
                entity: null //valeur déterminé à l'appel de la boite,
            }
        };
        var deleteItemData = {
            templateUrl: 'app/entities/point-vente/point-vente-delete-dialog.html',
            controller: 'PointVenteDeleteController',
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
        var entitySearchParams = function() {
            return {

            };
        };
        var entityParams = function() {
            return {

            };
        };


        var datas = {
            scope: $scope,
            vm: vm,
            entity: PointVente,
            dialogData: dialogData,
            entitySearch: PointVente,
            entitySearchParams: entitySearchParams,
            entityParams: entityParams
        };
        TableService.init(datas);
        
       

        vm.addUsers=function(pointVente){

            console.log("Liste des utilisateurs du point de vente");
            $uibModal.open({
                templateUrl: 'app/entities/point-vente/point-vente-users.html',
                controller: 'PointVenteUsersController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    pointVente:pointVente
                }
            }).result.then(function() {
            }, function() {
            });
            
        }
    
    }
})();