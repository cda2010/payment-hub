(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FacturierProfilController', FacturierProfilController);

        FacturierProfilController.$inject = ['$state','TableService', 'FacturierProfil','InitCrud','$scope'];

    function FacturierProfilController($state,TableService, FacturierProfil,InitCrud,$scope) {
        var vm = this;
        vm.title = "Liste des profils par facturier";
        vm.create = "Créer ou Modifier"
        vm.columns = [
            {
                name: 'typeService',
                title: 'Service'
            },
            {
            name: 'typeService',
            title: 'Etapes'
            },  
        ];
        vm.actions = {
            view: true,
            edit: true,
            delete: true
        };
        vm.getRandomColor = function() {
            var badgeClasses = ['primary','secondary','success','danger','warning','info','dark'];
            return badgeClasses[Math.floor(Math.random()*badgeClasses.length)];
        };

        var addItemData = {
            templateUrl: 'app/entities/facturier-profil/facturier-profil-dialog.html',
            controller: 'FacturierProfilDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: function () {
                    return {
                        facturier: null,
                        groupeProfilActeur: null,
                        id: null,
                        typeActeur: null
                    }; 
                }
            }
        };
        var dialogData = {
              addData: addItemData,
           // editData:   editItemData,
           // deleteData: deleteItemData,
           // detailData: detailItemData
        };

        /*var datas = {
            scope: $scope,
            vm: vm,
            entity: EtapeService,
            entitySearch: EtapeService,
            dialogData: dialogData,
            entitySearchParamsItem: {},
            entityParamsItem: {}
        };
        TableService.init(datas);*/

        var datas = {
            vm: vm,
            entityDatas: {
                typeActeur: null,
                id: null,
                facturier:null,
                groupeProfilActeur:null
            }, 
            entity: FacturierProfil,
            addTemplateUrl: "facturier-profil/facturier-profil-dialog.html",
            deleteTemplateUrl: "facturier-profil/facturier-profil-delete-dialog.html",
            controller: "FacturierProfil",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
