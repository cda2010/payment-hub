(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RegleCommissionController', RegleCommissionController);

    RegleCommissionController.$inject = ['InitCrud', '$scope', 'RegleCommission', 'RequestUtils', 'TypeService'];

    function RegleCommissionController(InitCrud, $scope, RegleCommission, RequestUtils, TypeService) {
        var vm = this;
        vm.title = "Regles de prise en charge des frais de transactions";
        vm.itemsFilter = [];
        RequestUtils.GetOrganisationByType.get({
            type: "FSE"
        }, function (datas) {
            vm.itemsFilter.push(
                [{
                    name: 'teneurSrcId',
                    model: null,
                    title: "Fse source",
                    datas: datas,
                    type: "select",
                    options: "data.id as data.libelleUniteOrganisation for data in col.datas"
                }, {
                    name: 'teneurDestId',
                    model: null,
                    title: "Fse Dest",
                    datas: datas,
                    type: "select",
                    options: "data.id as data.libelleUniteOrganisation for data in col.datas"
                }]
            );
            TypeService.query({}, function (datas) {
                vm.itemsFilter.push(
                    [{
                        name: 'typeServiceId',
                        model: null,
                        title: 'Choisir le type de service',
                        datas: datas,
                        type: "select",
                        options: "data.id as data.libelle for data in col.datas"
                    }]
                );
            });
        });

        vm.columns = [{
                name: 'typeOperateur',
                title: 'TypeOperateur'
            },
            {
                name: 'typeServiceLibelle',
                title: 'Type service'
            }, {
                name: 'teneurSrcLibelle',
                title: 'Teneur src'
            }, {
                name: 'teneurDestLibelle',
                title: 'Teneur dest'
            }, {
                name: 'activated',
                title: 'Statut',
                type: 'Boolean'
            }
        ];
        vm.actions = {
            view: false,
            edit: true,
            delete: true,
        };

        var datas = {
            vm: vm,
            entityDatas: {
                client: false,
                etiquette: null,
                typeOperateur: null,
                id: null
            },
            entity: RegleCommission,
            addTemplateUrl: "regle-commission/regle-commission-dialog.html",
            deleteTemplateUrl: "regle-commission/regle-commission-delete-dialog.html",
            controller: "RegleCommission",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope,
            hasFilter: true,
            items: vm.itemsFilter
        };
        InitCrud.init(datas);
    }
})();