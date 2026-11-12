(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeServiceReseauOrganisationController', TypeServiceReseauOrganisationController);

        TypeServiceReseauOrganisationController.$inject = ['$state','TableService', 'TypeServiceReseauOrganisation','InitCrud','$scope'];

    function TypeServiceReseauOrganisationController($state,TableService, TypeServiceReseauOrganisation,InitCrud,$scope) {
        var vm = this;
        vm.title = "Liste des etapes par services";
        vm.create = "Créer ou Modifier"
        vm.columns = [
            {
                name: 'typeCanal',
                title: 'Type Canal'
            },
            {
            name: 'application',
            title: 'Application'
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
            templateUrl: 'app/entities/type-service-reseau-organisation/type-service-reseau-organisation-dialog.html',
            controller: 'TypeServiceReseauOrganisationDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: function () {
                    return {
                        codePays: null,  
                        codePv: null,
                        uniteOrganisation: null,
                        typeService:null,
                        id: null
                    };
                }
            }
        };
        /**
         *Modification d'un élément
         */
         var editItemData = {
            templateUrl: 'app/entities/type-service-reseau-organisation/type-service-reseau-organisation-dialog.html',
            controller: 'TypeServiceReseauOrganisationDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: null
            }
        };

        /**
         * Suppresion d'un élément
         */
        var deleteItemData = {
            templateUrl: 'app/entities/type-service-reseau-organisation/type-service-reseau-organisation-delete-dialog.html',
            controller: 'TypeServiceReseauOrganisationDeleteController',
            controllerAs: 'vm',
            size: 'sm',  
            resolve: {
                entity: null
            }
        };
        var dialogData = {
            addData: addItemData,
          //  editData: editItemData,
            deleteData: deleteItemData,
            //detailData: detailItemData
        }; 
        var datas = {
            vm: vm,
            entityDatas: {
                codePays: null,  
                codePv: null,
                uniteOrganisation: null,
                typeService:null,
                id: null
            }, 
            entity: TypeServiceReseauOrganisation,
            addTemplateUrl: "type-service-reseau-organisation/type-service-reseau-organisation-dialog.html",
            deleteTemplateUrl: "type-service-reseau-organisation/type-service-reseau-organisation-delete-dialog.html",
            controller: "TypeServiceReseauOrganisation",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
