(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TableauRepartitionCommissionController', TableauRepartitionCommissionController);

    TableauRepartitionCommissionController.$inject = ['TypeService','TypeUOWithoutUR','InitCrud', '$scope', '$state', 'TableauRepartitionCommission','RequestUtils', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function TableauRepartitionCommissionController(TypeService,TypeUOWithoutUR,InitCrud, $scope, $state, TableauRepartitionCommission,RequestUtils, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.title = "Tableau de repartition commission";
        vm.state = 'tableau-ligne-repartition-commission({tableauRepartition:item})';
        vm.stateView = "tableau-ligne-repartition-commission";

        vm.statePrincipal="tableau-repartition-commission";

        vm.columns = [{
            name: 'niveaoOperation',
            title: 'Niveau',
            type:'Niveau'
        }, {
            name: 'typeServiceLibelle',
            title: 'TypeService'
        }
        , {
            name: 'groupLibelle',
            title: 'Groupe'
         }
            , {
            name: 'organisationLibelle',
            title: 'Acteur'
        }, {
            name: 'activated',
            title: 'Statut',
            type:'Boolean'
        }];

        vm.itemsFilter=[
        ];

        TypeUOWithoutUR.query({}, function (datas) {
            removeItem(datas,"libelleTypeUO","FSE");
            vm.itemsFilter.push(
                [
                    {
                        name: 'niveau',
                        model:null,
                        title: "Choisir le niveau",
                        datas:datas,
                        type:"select",
                        options:"data.niveau as data.libelleTypeUO for data in col.datas"
                    }
                ]
            );
            TypeService.query({},function(datas){
                vm.itemsFilter.push(
                    [
                        {
                            name: 'typeServiceId',
                            model:null,
                            title: 'Choisir le type de service',
                            datas:datas,
                            type:"select",
                            options:"data.id as data.libelle for data in col.datas"
                        }
                    ]
                );
            });
        });

        var removeItem = function (object, key, value) {
            if (value == undefined)
                return;
            for (var i in object) {
                if (object[i][key] == value) {
                    object.splice(i, 1);
                }
            }
        };

        vm.actions = {
            view: false,
            edit: true,
            delete: true,
            add:true
        };

        var datas={
            vm: vm,
             bodyClass:"col-sm-12",
            entityDatas: {},
            entity: RequestUtils.TableauRepartitionCommissionByCritere,
            addTemplateUrl: "tableau-repartition-commission/tableau-repartition-commission-dialog.html",
            deleteTemplateUrl: "tableau-repartition-commission/tableau-repartition-commission-delete-dialog.html",
            controller: "TableauRepartitionCommission",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope,
            detailsState: vm.state,
            stateView: vm.stateView,
            hasFilter:true,
            items:vm.itemsFilter,
            state:$state,
            statePrincipal:vm.statePrincipal,
            isMasterDetails: true
        };
        InitCrud.init(datas);
    }
})();
