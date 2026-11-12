(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UniteOrganisationController', UniteOrganisationController);

    UniteOrganisationController.$inject = ['$scope','PopupService', 'UniteOrganisation','TypeUniteOrganisation', '$TreeDnDConvert', 'TableService','isConsult' ];

    function UniteOrganisationController($scope,PopupService, UniteOrganisation, TypeUniteOrganisation, $TreeDnDConvert, TableService,isConsult ) {

        var vm = this;

        vm.organisations = UniteOrganisation.query();

        console.log('vm.organisations',vm.organisations);
        vm.organisation = [];
        vm.isConsult = isConsult;
        vm.id = null;
        vm.niveau = null;
        vm.createPointDeVente = createPointDeVente;
        vm.createUO = createUO;

        function createUO() {
            console.log('----Pere create----');
            var addItemData = {
            templateUrl: 'app/entities/unite-organisation/unite-organisation-dialog.html',
            controller: 'UniteOrganisationDialogController',
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
                        id: null,
                        libelleUniteOrganisation: null,
                        niveau: null,
                        feuille: true,
                        estSiege: null,
                        codeUO: null,
                        numeroOrdre: null,
                        representantId: null,
                        pereUOId: vm.id,
                        pereNiveau: vm.niveau,
                        typeUOId: null,
                        fournisseurId: null,
                        serviceProfilId:null
                    };
                }
            }
        };

            var editItemData = {
            templateUrl: 'app/entities/unite-organisation/unite-organisation-dialog.html',
            controller: 'UniteOrganisationDialogController',
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
            templateUrl: 'app/entities/unite-organisation/unite-organisation-dialog.html',
            controller: 'UniteOrganisationDialogController',
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
            templateUrl: 'app/entities/unite-organisation/unite-organisation-delete-dialog.html',
            controller: 'UniteOrganisationDeleteController',
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
            return {};
        };

            var datas = {
            scope: $scope,
            vm: vm,
            entity: UniteOrganisation,
            dialogData: dialogData,
            entitySearch: UniteOrganisation,
            entitySearchParams: entitySearchParams,
            entityParams: entityParams,
            loadCallBack: vm.load
        };
            TableService.init(datas);

    }

        function createPointDeVente(uniteOrganisationId) {

            var addItem ={
                templateUrl: 'app/entities/point-vente/point-vente-dialog.html',
                controller: 'PointVenteDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function(){
                        return false;
                    },
                    isSaveInUO: function(){
                        return true;
                    },
                    entity: function () {

                        return {
                            id:null,
                            codePointVente: null,
                            libellePointVente:null,
                            uniteOrganisationId : uniteOrganisationId
                        };
                    }
                }
            };

            var dialog = {
                addData: addItem
            };

            var datasEmpty=[];


            var pointVenteWindow = {
                vm: vm,
                datas: datasEmpty,
                dialogData: dialog,
                scope: $scope
            };
            PopupService.open(pointVenteWindow);
            vm.addItem();
        }

         vm.load = function() {
             UniteOrganisation.query({}, function (data) {
                 vm.organisation = data;
                 if (vm.organisation.length > vm.organisations.length) {
                     $scope.tree_data = $TreeDnDConvert.line2tree(vm.organisation, 'id', 'pereUOId');
                 }
                 else if (vm.organisation.length < vm.organisations.length) {
                     $scope.tree_data = $TreeDnDConvert.line2tree(vm.organisation, 'id', 'pereUOId');
                 }
                 else {
                     $scope.tree_data = $TreeDnDConvert.line2tree(vm.organisation, 'id', 'pereUOId');
                 }
             }, function () {
             });
         };

         vm.addChild = function(entity,niveau) {
             console.log('----Entrer----'+entity);
             //vm.createUO();
            vm.id=entity;
            vm.niveau = niveau;
            vm.addItem();
         };
        vm.detail = function(event,entity) {
            //vm.createUO();
            vm.detailItem(event,entity);
        };
        vm.edit = function(event,entity) {
            //vm.createUO();
            vm.editItem(event,entity);
        };
        vm.delete = function(event,entity) {
            //vm.createUO();
            console.log(entity);
            vm.deleteItem(event,entity);
        };

         vm.initInfo = function() {
             console.log('-----init');
            vm.loadAll();
        };



        //Parametrage de la librairie angular tree DND//

                vm.dataDemo=[];
                var tree         = {};
                $scope.tree_data = {};
                $scope.my_tree   = tree;
                $scope._filter            = {};
                $scope.expanding_property = {
                    /*template: "<td>OK All</td>",*/
                    field:       'Nom',
                    titleClass:  'text-center',
                    cellClass:   'v-middle',
                    displayName: 'libelleUniteOrganisation'
                };
       if(vm.isConsult){
                $scope.col_defs = [
                    {
                        field: 'CodeUO',
                         displayName:'codeUO'
                    },
                    {
                        field: 'Ordre',
                        displayName:'numeroOrdre'
                    },
                    {
                        field: 'TypeUO',
                        displayName: 'typeUOLibelleTypeUO'
                    }, {
                        titleStyle:   {
                            'width': '25pt'
                        },
                        titleClass:   'text-center',
                        cellClass:    'v-middle text-center',
                        displayName:  'Actions',
                        field:  'Actions',
                        id: 'id',
                        cellTemplate:' <button type="button" title="Consulter" ng-click="vm.detail($event,node)" class="btn btn-primary shiny btn-xs btn-circle"> <span class="glyphicon glyphicon-eye-open"></span> </button>'
                        }];

                $scope.expanding_property_inside = {
                    /*template: "<td>OK All</td>",*/
                    field:         'Nom',
                    titleClass:    'text-center',
                    titleTemplate: '<label> {{expandingProperty.field || expandingProperty}} <input autocomplete="off"class="form-control input-xs" ng-model="_filter.libelleUniteOrganisation"></label>',
                    cellClass:     'v-middle',
                    displayName:   'libelleUniteOrganisation'
                };
                $scope.col_defs_inside = [
                    {
                        field:         'CodeUO',
                        titleClass:    'text-center',
                         displayName:   'codeUO',
                        titleTemplate: '<label> {{col.field}} <input autocomplete="off"class="form-control input-xs" ng-model="_filter.codeUO"></label>'
                    },
                    {
                        field:         'Ordre',
                        titleClass:    'text-center',
                        displayName:   'numeroOrdre',
                        titleTemplate: '<label> {{col.field}} <input autocomplete="off"class="form-control input-xs" ng-model="_filter.numeroOrdre"></label>'
                    },
                    {
                        field:         'TypeUO',
                        titleClass:    'text-center',
                        displayName:   'typeUOLibelleTypeUO',
                        titleTemplate: '<label> {{col.field}} <input autocomplete="off"class="form-control input-xs" ng-model="_filter.typeUOLibelleTypeUO"></label>'
                    }, {
                        titleStyle:    {
                            'width': '25pt'
                        },
                        titleClass:    'text-center',
                        titleTemplate: '<label> {{col.displayName}} <input autocomplete="off"class="form-control" ng-hide="true" ng-model="_filter.description"></label>',
                        cellClass:     'v-middle text-center',
                        displayName:   'Actions',
                        cellTemplate:' <button type="button" title="Consulter" ng-click="vm.detail($event,node)" class="btn btn-primary shiny btn-xs btn-circle"> <span class="glyphicon glyphicon-eye-open"></span> </button>'
                }];
       }else{
           $scope.col_defs = [
               {
                   field: 'CodeUO',
                   displayName:'codeUO'
               },
               {
                   field: 'Ordre',
                   displayName:'numeroOrdre'
               },
               {
                   field: 'TypeUO',
                   displayName: 'typeUOLibelleTypeUO'
               }, {
                   titleStyle:   {
                       'width': '120pt'
                   },
                   titleClass:   'text-center',
                   cellClass:    'v-middle text-center',
                   displayName:  'Actions',
                   field:  'Actions',
                   id: 'id',
                   cellTemplate: '<button type="button" ng-if="node.withPV" title="Ajouter des informations Complementaires" ui-sref="unite-organisation.edit({id:node.id})" class="btn btn-primary shiny btn-xs btn-circle"> <span class="fa fa fa-cog"></span> </button>' +
                   '  <button type="button" title="Ajouter" ng-click="vm.addChild(node.id,node.niveau)" class="btn btn-primary shiny btn-xs btn-circle"> <span class="glyphicon glyphicon-plus"></span> </button> ' +
                   ' <button type="button" title="Consulter" ng-click="vm.detail($event,node)" class="btn btn-primary shiny btn-xs btn-circle"> <span class="glyphicon glyphicon-eye-open"></span> </button>' +
                   ' <button type="button" title="Modifier" ng-click="vm.edit($event,node)" class="btn btn-success shiny btn-xs btn-circle"> <span class="glyphicon glyphicon-pencil"></span> </button>' +
                   ' <button type="button" title="Supprimer" ng-click="vm.delete($event,node)" class="btn btn-danger shiny btn-xs btn-circle"> <span class="glyphicon glyphicon-remove-circle"></span> </button>'
               }];

           $scope.expanding_property_inside = {
               /*template: "<td>OK All</td>",*/
               field:         'Nom',
               titleClass:    'text-center',
               titleTemplate: '<label> {{expandingProperty.field || expandingProperty}} <input autocomplete="off"class="form-control input-xs" ng-model="_filter.libelleUniteOrganisation"></label>',
               cellClass:     'v-middle',
               displayName:   'libelleUniteOrganisation'
           };
           $scope.col_defs_inside = [
               {
                   field:         'CodeUO',
                   titleClass:    'text-center',
                   displayName:   'codeUO',
                   titleTemplate: '<label> {{col.field}} <input autocomplete="off"class="form-control input-xs" ng-model="_filter.codeUO"></label>'
               },
               {
                   field:         'Ordre',
                   titleClass:    'text-center',
                   displayName:   'numeroOrdre',
                   titleTemplate: '<label> {{col.field}} <input autocomplete="off"class="form-control input-xs" ng-model="_filter.numeroOrdre"></label>'
               },
               {
                   field:         'TypeUO',
                   titleClass:    'text-center',
                   displayName:   'typeUOLibelleTypeUO',
                   titleTemplate: '<label> {{col.field}} <input autocomplete="off"class="form-control input-xs" ng-model="_filter.typeUOLibelleTypeUO"></label>'
               }, {
                   titleStyle:    {
                       'width': '120pt'
                   },
                   titleClass:    'text-center',
                   titleTemplate: '<label> {{col.displayName}} <input autocomplete="off"class="form-control" ng-hide="true" ng-model="_filter.description"></label>',
                   cellClass:     'v-middle text-center',
                   displayName:   'Actions',
                   cellTemplate: '<button type="button" ng-if="node.withPV" title="Ajouter des informations Complementaires" ui-sref="unite-organisation.edit({id:node.id})" class="btn btn-primary shiny btn-xs btn-circle"> <span class="fa fa fa-cog"></span> </button>' +
                   '  <button type="button" title="Ajouter" ng-click="vm.addChild(node.id,node.niveau)" class="btn btn-primary shiny btn-xs btn-circle"> <span class="glyphicon glyphicon-plus"></span> </button> ' +
                   ' <button type="button" title="Consulter" ng-click="vm.detail($event,node)" class="btn btn-primary shiny btn-xs btn-circle"> <span class="glyphicon glyphicon-eye-open"></span> </button>' +
                   ' <button type="button" title="Modifier" ng-click="vm.edit($event,node)" class="btn btn-success shiny btn-xs btn-circle"> <span class="glyphicon glyphicon-pencil"></span> </button>' +
                   ' <button type="button" title="Supprimer" ng-click="vm.delete($event,node)" class="btn btn-danger shiny btn-xs btn-circle"> <span class="glyphicon glyphicon-remove-circle"></span> </button>'
               }];
       }
                        vm.createUO();


    }
})();
